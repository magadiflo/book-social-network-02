import { Component, EventEmitter, Input, Output } from '@angular/core';

import { BookResponse } from '../../../services/models';
import { BookImagePipe } from '../../pipes/book-image.pipe';
import { RatingComponent } from './../rating/rating.component';

@Component({
  selector: 'book-card',
  standalone: true,
  imports: [RatingComponent, BookImagePipe],
  templateUrl: './book-card.component.html',
  styleUrl: './book-card.component.scss'
})
export class BookCardComponent {

  private _book!: BookResponse;
  private _manage = false;

  @Output() private share: EventEmitter<BookResponse> = new EventEmitter<BookResponse>();
  @Output() private archive: EventEmitter<BookResponse> = new EventEmitter<BookResponse>();
  @Output() private addToWaitingList: EventEmitter<BookResponse> = new EventEmitter<BookResponse>();
  @Output() private borrow: EventEmitter<BookResponse> = new EventEmitter<BookResponse>();
  @Output() private edit: EventEmitter<BookResponse> = new EventEmitter<BookResponse>();
  @Output() private details: EventEmitter<BookResponse> = new EventEmitter<BookResponse>();

  @Input({ required: true })
  public set book(book: BookResponse) {
    this._book = book;
  }

  @Input()
  public set manage(value: boolean) {
    this._manage = value;
  }

  public get book(): BookResponse {
    return this._book;
  }

  public get manage(): boolean {
    return this._manage;
  }

  public onShowDetails(): void {
    this.details.emit(this._book);
  }

  public onBorrow(): void {
    this.borrow.emit(this._book);
  }

  public onAddToWaitingList(): void {
    this.addToWaitingList.emit(this._book);
  }

  public onEdit(): void {
    this.edit.emit(this._book);
  }

  public onShare(): void {
    this.share.emit(this._book);
  }

  public onArchive(): void {
    this.archive.emit(this._book);
  }

}
